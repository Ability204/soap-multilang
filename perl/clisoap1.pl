#!/usr/bin/perl
# cpanm LWP::UserAgent HTTP::Daemon
# perl clisoap1.pl
# http://localhost:8083/?n=10
use strict;
use warnings;
use HTTP::Daemon;
use LWP::UserAgent;
use URI::Escape;

my $d = HTTP::Daemon->new(LocalPort => 8083, ReuseAddr => 1)
    or die "Cannot start server: $!";

print "Listening on http://localhost:8083/\n";

while (my $c = $d->accept) {
    while (my $r = $c->get_request) {
        my $uri  = $r->uri;
        my %params = map { split /=/, $_, 2 } split /&/, ($uri->query // '');
        my $n = $params{n} // '10';

        my $soap = <<XML;
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
      <ubiNum>$n</ubiNum>
    </NumberToWords>
  </soap:Body>
</soap:Envelope>
XML

        my $ua = LWP::UserAgent->new(ssl_opts => { verify_hostname => 0 });
        my $resp = $ua->post(
            'https://www.dataaccess.com/webservicesserver/NumberConversion.wso',
            Content_Type => 'text/xml; charset=utf-8',
            Content      => $soap,
        );

        my $result = '';
        $result = $1 if $resp->content =~ /<NumberToWordsResult>(.*?)<\/NumberToWordsResult>/s;

        $c->send_response(HTTP::Response->new(200, 'OK',
            ['Content-Type' => 'text/plain'], $result));
    }
    $c->close;
}
