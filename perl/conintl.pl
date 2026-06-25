#!/usr/bin/perl
# cpanm HTTP::Daemon
# perl conintl.pl
# http://localhost:8083/?n=10
use strict;
use warnings;
use HTTP::Daemon;

my @ONES = ('', qw(uno dos tres cuatro cinco seis siete ocho nueve diez
                   once doce trece catorce quince dieciséis diecisiete dieciocho diecinueve));
my @TENS = ('', '', qw(veinte treinta cuarenta cincuenta sesenta setenta ochenta noventa));
my @HUND = ('', qw(ciento doscientos trescientos cuatrocientos quinientos
                   seiscientos setecientos ochocientos novecientos));

sub to_words_es {
    my ($n) = @_;
    return 'cero'      if $n == 0;
    return $ONES[$n]   if $n < 20;
    if ($n < 100) {
        return $TENS[$n / 10] if $n % 10 == 0;
        return "$TENS[${\int($n/10)}] y $ONES[$n % 10]";
    }
    if ($n < 1000) {
        return 'cien' if $n == 100;
        my $rest = to_words_es($n % 100);
        return $rest ? "$HUND[${\int($n/100)}] $rest" : $HUND[int($n/100)];
    }
    return "$n";
}

my $d = HTTP::Daemon->new(LocalPort => 8083, ReuseAddr => 1)
    or die "Cannot start server: $!";

print "Listening on http://localhost:8083/\n";

while (my $c = $d->accept) {
    while (my $r = $c->get_request) {
        my %params = map { split /=/, $_, 2 } split /&/, ($r->uri->query // '');
        my $n = int($params{n} // 10);
        $c->send_response(HTTP::Response->new(200, 'OK',
            ['Content-Type' => 'text/plain; charset=utf-8'], to_words_es($n)));
    }
    $c->close;
}
