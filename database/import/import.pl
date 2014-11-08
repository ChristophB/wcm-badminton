#!/usr/bin/perl

use strict;
use Carp;
use DateTime::Format::Strptime;
use Data::Dumper;
use DBI;
use constant {
    USER => 'postgres',
    PASS => 'password',
    DB   => 'dbi:Pg:dbname=testdb;host=localhost'
};

$Data::Dumper::Sortkeys = 1;

foreach my $file (<../../crawler/data/*.html>) {    
    my $profile_start = 0;
    my $biodata_start = 0;
    my $athlete_start = 0;
    my %data;
    
    print 'Working on: '. $file. "\n";
    open (FILE, $file) or croak('Error: Could not open $file');
    
    while (<FILE>) {
	chomp;
	$profile_start = 1 if ($_ =~ /profileheader/);
	$biodata_start = 1 if ($_ =~ /Biodata/);
	$athlete_start = 1 if ($_ =~ /Athlete Profile/);
	next unless ($profile_start);
	
	if ($_ =~ /<h3 title="/) {
	    ($data{firstname}, $data{name}) = getName(extractAttribute($_, 'title'));
	    $data{nationality} = extractAttribute($_, 'alt');
	}
	$data{id}     = extractId($_) if ($_ =~ /overview\.aspx/g);
	$data{image}  = extractAttribute($_, 'src') if ($_ =~ /cphPage_cphPage_imgPlayerImage/);
	$data{gender} = 
	    !$data{gender} ? $_ =~ /Men's (singles|doubles):/g ? 'm' 
	    : $_ =~ /Women's (singles|doubles):/g ? 'f'
	    : undef 
	    : $data{gender}
	    ;

	if ($biodata_start && $_ =~ /li/) {
	    %data = (%data, extractBiodata($_));
	    $biodata_start = 0;
	}
	if ($athlete_start && $_ =~ /li/) {
	    %data = (%data, extractAthlete($_));
	    $athlete_start = 0;
	}
    }
    $data{gender} = $data{gender} ? $data{gender} : 'u';
    print Dumper(\%data);
    insertData(\%data);
}

sub getName {
    my $string = shift;
    my @names = split ' ', $string;
    my $part;
    my $switch;
    my @firstname;
    my @name;

    foreach (@names) {
	$part = 2 if (!$part && $_ eq uc $_);
	$part = 1 if (!$part && $_ ne uc $_); 
	$switch = 1 and $part = 1 if ($part == 2 && $_ ne uc $_ && !$switch);
	$switch = 1 and $part = 2 if ($part == 1 && $_ eq uc $_ && !$switch);
	push @name, $_ if (($switch || $_ eq uc $_) && $part == 2);
	push @firstname, $_ if (($switch || $_ ne uc $_) && $part == 1);
    }
    return ((join ' ', @firstname), (join ' ', @name));
}

sub extractAttribute {
    my $line      = shift or croak('Error: Parameter $line missing!');
    my $attribute = shift or croak('Error: Parameter $attribute missing!');
    my $value     = ($line =~ /$attribute=".*?"/g)[0];
    
    $value =~ s/$attribute="|"//g;
    return $value;
}

sub extractId {
    my $line = shift or croak('Error: Parameter $line missing!');
    my $id = ($line =~ /overview\.aspx.*?"/g)[0];
    
    $id =~ s/[^\dA-Z\-]*//g;
    return $id;
}

sub extractBiodata {
    my $line = shift or croak('Error: Parameter $line missing!');
    my %hash = (
	birthdate             => 'DOB'
	, birthplace_city     => 'POB'
	, birthplace_state    => 'POB'
	, height              => 'Height'
	, facebook            => 'Facebook'
	, twitter             => 'Twitter'
	, website             => 'Website'
	, cur_residence_city  => 'CurrentResidence'
	, cur_residence_state => 'CurrentResidence'
	, occupation          => 'Occupation'
	, hobbies             => 'Hobbies'
	, education           => 'EducationLevel'
	, family              => 'FamilyInformation'
	, style               => 'StyleOfPlay'
	, otherSports         => 'OtherSports' 
	, familySporting      => 'FamousSportingRelatives'
	, languages           => 'Languages'
	, debut_year          => 'InternationalDebut' 
	, debut_event         => 'InternationalDebut'
	, nickname            => 'Nickname'
	, club                => 'Club'
	, coach               => 'Coach' #evtl. unterteilen in vor- und nachname
	#, majorInjuries
	);

    %hash = addElementsToHash($line, \%hash);
    $hash{hand}                = extractHand($line);
    $hash{birthdate}           = getDate($hash{birthdate});
    $hash{birthplace_city}     = (split ', ', $hash{birthplace_city})[0];
    $hash{birthplace_state}    = (split ', ', $hash{birthplace_state})[1];
    $hash{cur_residence_city}  = (split ', ', $hash{cur_residence_city})[0];
    $hash{cur_residence_state} = (split ', ', $hash{cur_residence_state})[1];
    $hash{languages}           = [split ', ', $hash{languages}];
    $hash{debut_year}          = (split ', ', $hash{debut_year})[0];
    $hash{debut_event}         = (split ', ', $hash{debut_event})[0];
    return %hash;
}

sub extractAthlete {
    my $line = shift or croak('Erro: Parameter $line missing!');
    my %hash =  (
	beginSport          => 'BeginSport'
	, sponsor           => 'EquipmentSponsor'
	, regime            => 'TrainingRegime'
	, achivements       => 'MemorableAchievements'
	, influentialPerson => 'MostInfluentialPerson'
	, idol              => 'SportingHero'
	, rituals           => 'SuperstitionsRituals'
	, philosophy        => 'SportingPhilosophy'
	, awards            => 'SportingAwards'
	, ambitions         => 'SportingAmbitions'
	, startCompetitive  => 'StartPlayingCompetitively'
	, teammemberSince   => 'MemberNationalTeamSince'
	#,previousOlympics
	);
    
    return addElementsToHash($line, \%hash);
}

sub insertData {
    my $data = shift or croak('Error: Parameter %data missing!'); 
    my $dbh  = DBI->connect(
	DB, USER, PASS
	, {AutoCommit => 0, RaiseError => 1, PrintError => 0}
	) or croak('Error: DB-Connection failed!');

    $dbh->do(
	'INSERT INTO players'. "\n"
	. 'VALUES (?, ?, ?, ?, ?)'
	, undef
	, $data->{id}
	, $data->{firstname}
	, $data->{name}
	, $data->{birthdate}
	, $data->{gender}
	) or $dbh->errstr;
    $dbh->commit;
}

sub addElementsToHash {
    my $line = shift or croak('Error: Parameter $line missing!');
    my $hash = shift or croak('Error: Parameter $hash missing!');

    foreach my $key (keys %$hash) {
	$hash->{$key} = extractElement($line, $hash->{$key});
    }
    return %$hash;
}

sub extractHand {
    my $line  = shift or croak('Error: Parameter $line missing!');
    my $value = ($line =~ /Plays:<\/.*?\/label/g)[0]; 
    my $value = ($value =~ /readonly">.*?</g)[0];
    
    $value =~ s/readonly">|<//g;
    return 'right' if ((lc $value) =~ /right/g);
    return 'left' if ((lc $value) =~ /left/g);
    return 'unknown';
}

sub getDate {
    my $string = shift or return;
    my $strp   = DateTime::Format::Strptime->new(pattern => '%d.%m.%Y');
    my $dt     = $strp->parse_datetime($string);
    
    return undef unless defined $dt;
    return $dt->ymd;
}

sub extractElement {
    my $line  = shift or croak('Error: Parameter $line missing!');
    my $id    = shift or croak('Error: Parameter $id missing!');
    my $value = ($line =~ /id="$id">.*?</g)[0];
    
    $value =~ s/id="$id">|<//g;
    return $value;
}
