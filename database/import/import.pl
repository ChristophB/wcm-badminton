#!/usr/bin/perl
use strict;
use Carp;
use DateTime::Format::Strptime;
use Data::Dumper;
use File::Basename;
use lib dirname (__FILE__) . "/lib";

use Databasehandler;
use Parser;

my $players_counter = 0;

chdir dirname(__FILE__);
$Data::Dumper::Sortkeys = 1;

foreach my $file (<../../crawler/data/*>) {
	my $profile_start = 0;
	my $biodata_start = 0;
	my $athlete_start = 0;
	my $file_name     = (split '/', $file)[-1];
	my %data;
	my @disciplines;
	
	next unless ($file_name =~ /id=[A-Z0-9]*?-[A-Z0-9]*?-[A-Z0-9]*?-[A-Z0-9]*?-[A-Z0-9]*?$/g);
	#print 'Working on: '. $file_name. "\n";
	open (FILE, $file) or croak('Error: Could not open $file');
	$data{id} = (split 'id=', $file)[-1];
	while (<FILE>) {
		chomp;
		my $line = $_;
		$profile_start = 1 if ($_ =~ /profileheader/);
		$biodata_start = 1 if ($_ =~ /Biodata/);
		$athlete_start = 1 if ($_ =~ /Athlete Profile/);
		
		if ($line =~ /(Men's|Women's|Mixed) (Singles|Doubles)/g) {
			push @disciplines, 'Men\'s Singles'   if ($line =~ /Men's Singles/);
			push @disciplines, 'Men\'s Doubles'   if ($line =~ /Men's Doubles/);
			push @disciplines, 'Women\'s Singles' if ($line =~ /Women's Singles/);
			push @disciplines, 'Women\'s Doubles' if ($line =~ /Women's Doubles/);
			push @disciplines, 'Mixed Doubles'    if ($line =~ /Mixed Doubles/);
			@disciplines = uniq(@disciplines);
		}
		
		next unless ($profile_start);
		
		if ($_ =~ /<h3 title="/) {
			($data{firstname}, $data{name}) = getName(extractAttribute($_, 'title'));
			$data{nationality} = extractAttribute($_, 'alt');
		}
		$data{image}  = extractAttribute($_, 'src') if ($_ =~ /cphPage_cphPage_imgPlayerImage/);
		$data{gender} = 
		  !$data{gender} ? $_ =~ /Men's (Singles|Doubles):/g ? 'm' 
		    : $_ =~ /Women's (Singles|Doubles):/g ? 'f'
		      : undef 
		        : $data{gender}
		          ;

		if ($biodata_start && $_ =~ /li/) {
			%data = (%data, extractBiodata($_));
			$biodata_start = 0;
		}
		if ($athlete_start && $_ =~ /li/) {
			%data = (%data, extractAthlete($_, $data{birthdate}));
			$athlete_start = 0;
			$profile_start = 0;
		}
	}
	$data{disciplines} = \@disciplines;
	$data{gender} = $data{gender} ? $data{gender} : 'u';
	$players_counter++ if (insertData(\%data));
	
	#print Dumper(\%data);
	#exit;
}

finishTransaction();
print $players_counter. ' new Players inserted.'. "\n\n";

sub getName {
	my $string = shift;
	my @names  = split ' ', $string;
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
	
	if (!@name || !@firstname) {
		@firstname = @names[0 .. ($#names-1)];
		@name      = @names[-1];
	}
	return ((join ' ', @firstname), (join ' ', @name));
}

sub getDate {
	my $string = shift or return;
	my $strp;
	my $dt;
	
	$strp = DateTime::Format::Strptime->new(pattern => '%d.%m.%Y')
	  if ($string =~ /\./g);
	$strp = DateTime::Format::Strptime->new(pattern => '%m/%d/%Y')
	  if ($string =~ /\//g);
	$dt = $strp->parse_datetime($string);
	return defined $dt ? $dt->ymd : undef;
}

sub uniq {
	my %seen;
	grep !$seen{$_}++, @_;
}

1;
