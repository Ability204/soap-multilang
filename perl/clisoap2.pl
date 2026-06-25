#!/usr/bin/perl
# cpanm LWP::UserAgent HTTP::Daemon JSON
# perl clisoap2.pl
# http://localhost:8083/?n=10
use strict;
use warnings;
use HTTP::Daemon;
use LWP::UserAgent;
use URI::Escape;
use JSON;

sub translate_to_es {
    my ($text) = @_;
    my $encoded = uri_escape($text);
    my $url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q=$encoded";
    my $ua = LWP::UserAgent->new(ssl_opts => { verify_hostname => 0 });
    my $resp = $ua->get($url);
    my $data = decode_json($resp->content);
    return $data->[0][0][0];
}

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

        my $word = '';
        $word = $1 if $resp->content =~ /<NumberToWordsResult>(.*?)<\/NumberToWordsResult>/s;
        $word =~ s/^\s+|\s+$//g;

        my $translated = translate_to_es($word);

        $c->send_response(HTTP::Response->new(200, 'OK',
            ['Content-Type' => 'text/plain; charset=utf-8'], $translated));
    }
    $c->close;
}
