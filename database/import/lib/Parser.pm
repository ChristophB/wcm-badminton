#!usr/bin/perl
use strict;
use Carp;

sub extractAttribute {
    my $line      = shift or croak('Error: Parameter $line missing!');
    my $attribute = shift or croak('Error: Parameter $attribute missing!');
    my $value     = ($line =~ /$attribute=".*?"/g)[0];
    
    $value =~ s/$attribute="|"//g;
    return $value && $value ne '' ? $value : undef;
}

# sub extractId {
#     my $line = shift or croak('Error: Parameter $line missing!');
#     my $id   = ($line =~ /overview\.aspx.*?"/g)[0];
    
#     $id =~ s/[^\dA-Z\-]*//g;
#     return $id && $id ne '' ? $id : undef;
# }

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
	, other_sports        => 'OtherSports' 
	, family_sporting     => 'FamousSportingRelatives'
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
    $hash{languages}           = [split ', |,| and ', $hash{languages}];
    #$hash{debut_event}         = (split ' ', $hash{debut_event}, 2)[1];
    $hash{debut_year}          = ($hash{debut_year} =~ /\d\d\d\d/g)[0];
    return %hash;
}

sub extractAthlete {
    my $line = shift or croak('Erro: Parameter $line missing!');
    my $birthdate = shift;
    my %hash =  (
	beginSport           => 'BeginSport'
	, sponsor            => 'EquipmentSponsor'
	, regime             => 'TrainingRegime'
	, achivements        => 'MemorableAchievements'
	, influential_person => 'MostInfluentialPerson'
	, idol               => 'SportingHero'
	, rituals            => 'SuperstitionsRituals'
	, philosophy         => 'SportingPhilosophy'
	, awards             => 'SportingAwards'
	, ambitions          => 'SportingAmbitions'
	, start_competitive  => 'StartPlayingCompetitively'
	, teammember_since   => 'MemberNationalTeamSince'
	#,previousOlympics
	);
    
    %hash = addElementsToHash($line, \%hash);
    $hash{start_competitive} = extractYear($hash{start_competitive}, $birthdate);
    $hash{teammember_since}  = extractYear($hash{teammember_since}, $birthdate);
    return %hash;
}

sub extractYear {
    my $string    = shift or return;
    my $birthdate = shift;
    
    if ($string =~ /\d\d\d\d/) {
	$string = ($string =~ /\d\d\d\d/g)[0];
    } elsif ($string =~ /[Yy]ear|[Aa]ge|[Ii] [Ww]as/) {
	$string = ($string =~ /[., ]\d\d?[., ]/g)[0];
	$string =~ s/[., ]//g;
    } else {
	$string = undef;
    }
    
    if ($birthdate && $string =~ /\d\d?/ && !($string =~ /\d\d\d\d/)) {
	$string = (split '-', $birthdate)[0] + $string;
    }
    return $string;
}

sub extractHand {
    my $line  = shift or croak('Error: Parameter $line missing!');
    #my $value = ($line =~ /Play R or L:<\/.*?\/label/g)[0]; new Website-Version
    my $value = ($line =~ /Plays:<\/.*?\/label/g)[0];
    my $value = ($value =~ /readonly">.*?</g)[0];
    
    $value =~ s/readonly">|<//g;
    return 'right' if ((lc $value) =~ /right/g); 
    return 'left'  if ((lc $value) =~ /left/g); 
    return 'unknown';
}

sub extractElement {
    my $line  = shift or croak('Error: Parameter $line missing!');
    my $id    = shift or croak('Error: Parameter $id missing!');
    my $value = ($line =~ /id="$id">.*?</g)[0];
    
    $value =~ s/id="$id">|<//g;
    return $value && $value ne '' ? $value : undef;
}

sub addElementsToHash {
    my $line = shift or croak('Error: Parameter $line missing!');
    my $hash = shift or croak('Error: Parameter $hash missing!');

    foreach my $key (keys %$hash) {
	$hash->{$key} = extractElement($line, $hash->{$key});
    }
    return %$hash;
}

1;
