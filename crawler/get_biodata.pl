#!/usr/bin/perl

use strict;
use Carp;

foreach my $file (<data_m/*.html>) {
    next unless ($file =~ /id_[A-Z0-9]*?-[A-Z0-9]*?-[A-Z0-9]*?-[A-Z0-9]*?-[A-Z0-9]*?\.html$/g);
    my $id  = (split 'id_|\.html', $file)[-1];
    my $url = 'http://bwf.tournamentsoftware.com/profile/biography.aspx?id='. $id;

    system("wget '$url' -P data/");
}
