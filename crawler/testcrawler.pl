#!/usr/bin/perl 
use 5.010;
use strict;
use utf8;
use Mojo::UserAgent;

open OUT, '>>', 'url.csv' or die $!;
close OUT;

my @rankings;
my $counter;
my $method = 0;
my @urls     = map { Mojo::URL->new($_) } 'http://bwf.tournamentsoftware.com/rankings.aspx';
my $max_conn = 2;
my $active   = 0;
my $ua       = Mojo::UserAgent->new(max_redirects => 5);
    
$ua->proxy->detect;

Mojo::IOLoop->recurring(
    0 => sub {
        for ($active + 1 .. $max_conn) {
            
            return ($active or Mojo::IOLoop->stop)
                unless my $url = shift @urls;
                
            # Fetch non-blocking just by adding
            # a callback and marking as active
            ++$active;
            $ua->get($url => \&get_callback);
        }
    }
);

Mojo::IOLoop->start unless Mojo::IOLoop->is_running;
Mojo::IOLoop->reset;


$method = 1;

foreach (@rankings) {
    @urls     = map { Mojo::URL->new($_) } $_;
    $active   = 0;
    $ua       = Mojo::UserAgent->new(max_redirects => 5);
    
    $ua->proxy->detect;
 
    Mojo::IOLoop->recurring(
        0 => sub {
            for ($active + 1 .. $max_conn) {
                
                return ($active or Mojo::IOLoop->stop)
                    unless my $url = shift @urls;
                
                # Fetch non-blocking just by adding
                # a callback and marking as active
                ++$active;
                $ua->get($url => \&get_callback);
            }
        }
    );
    
    Mojo::IOLoop->start unless Mojo::IOLoop->is_running;
    Mojo::IOLoop->reset;
}

    
sub get_callback {
    my (undef, $tx) = @_;
    --$active;
    
    return if not $tx->res->is_status_class(200)
        or $tx->res->headers->content_type !~ m{^text/html\b}ix;
    
    my $url = $tx->req->url;
    
    #if(!$method) {
        #parse_html_prepare($url, $tx);
    #} else {
        parse_html($url, $tx);
    #}
    
    return;
}

sub parse_html {
    open OUT, '>>', 'id.csv';
    my ($url, $tx) = @_;
    
    for my $e ($tx->res->dom('a[href]')->each) {
        my $link = Mojo::URL->new($e->{href});
        $counter++;
        next if 'Mojo::URL' ne ref $link;
        
        $link = $link->to_abs($tx->req->url)->fragment(undef);
        
        
        next unless grep { $link->protocol eq $_ } qw(http https);
        next if @{$link->path->parts} > 3 or @{$link->path->parts} < 2;
        
        # Access every link only once
        state $uniq = {};
        ++$uniq->{$url->to_string};
        next if ++$uniq->{$link->to_string} > 1;
        next if $link->host ne $url->host;
        next unless (
            $link =~ /\/profile\/default\.aspx/
                || $link =~ /\/ranking\/ranking\.aspx/
                    || $link =~ /\/ranking\/category\.aspx/
                );
        
        if ($link =~ /\/profile\/default\.aspx/) {
            my (undef, $id) = split 'id=', $link;
            print OUT 'http://bwf.tournamentsoftware.com/profile/biography.aspx?id='. $id. "\n";
            #print $id. "\n";
            
            next;
        }
        
        push @urls, $link;
    }
    print $counter. "\n";
    close OUT;
    return;
}

# sub parse_html_prepare {
#     my ($url, $tx) = @_;
    
#     for my $e ($tx->res->dom('a[href]')->each) {
#         my $link = Mojo::URL->new($e->{href});
#         next if 'Mojo::URL' ne ref $link;
        
#         $link = $link->to_abs($tx->req->url)->fragment(undef);
#         next unless grep { $link->protocol eq $_ } qw(http https);
#         next if @{$link->path->parts} > 3;
        
#         # Access every link only once
#         state $uniq = {};
#         ++$uniq->{$url->to_string};
#         next if ++$uniq->{$link->to_string} > 1;
#         next if $link->host ne $url->host;
#         next unless ($link =~ /\/ranking\/ranking\.aspx\?rid/);
        
#         push @urls, $link;
#     }
    
#     if ($url->to_string =~ /\/ranking\/ranking\.aspx\?rid/) {
#         for my $e ($tx->res->dom('option[value]')->each) {
#             my $link_ranking = $url->to_string;
#             my $value = $e->{value};
#             $link_ranking =~ s/rid=.*/id=$value/;
#             push @rankings, $link_ranking;
#         }
#     }
#     return;
# }
