#!/usr/bin/perl -w

use strict;
use Carp;

my @files = <../../crawler/data/*.html>;
my $profil_start = 0;

foreach my $file (@files) {
    print 'Working on: '. $file. "\n";
    open (FILE, $file);

    while (<FILE>) {
	chomp;
	$profil_start = 1 if ($_ =~ /profileheader/);
	next unless ($profil_start);
	
	my $image = extractAttribute($_, 'src') if ($_ =~ /cphPage_cphPage_imgPlayerImage/);
	if ($_ =~ /<h3 title="/) {
	    my ($firstname, $name) = extractName($_);
	    my $nationality = extractAttribute($_, 'alt');
	}
    }
}

sub extractName {
    my $value = extractAttribute(shift, 'title');
    my @array = split ' ', $value;
    return ((join ' ', @array[0 .. $#array - 1]), $array[-1]);
}

sub extractAttribute {
    my $line = shift or croak('Error: Parameter $line missing!');
    my $attribute = shift or croak('Error: Parameter $attribute missing!');
    my $value = ($line =~ /$attribute=".*?"/g)[0];
    $value =~ s/$attribute="|"//g;
    return $value;
}

sub insertData {
    
}
