#!/usr/bin/perl

use IO::Socket::INET;
use Getopt::Long;

sub ws_timeout() {
	$::ctrlC = 1;
	die("web server response timeout\n");
}

$::ctrlC = 0;

my $ws_server;
my $xml_f;

my $filter = undef;
my $prettier = undef;
my $dump_req = undef;
my $header_out = "";
my $content_out = "";
my @caps = ();


sub usage() {
	print <<USG;
usage: ws_client [-h] -x <ws call> -s <server> 
       -x <ws_call>: a file <ws_call>.xml must exist in the working directory 
       -s <server>: address:port
       -h: prints this message
  Long options:
       --prettier: produce more palatable output
       --dump_req: dump XML request
       --filter <string>: filters the capabilities (unimplemented)
       --caps <URN string [URN string [URN string]]>: list capability URNs
       --help: prints this message
USG
	exit;
}

if(!GetOptions ('x=s' =>		\$xml_f,
		's=s' =>                \$ws_server,
		'prettier' =>         	\$prettier,
		'dump_req' =>         	\$dump_req,
		'filter=s' =>         	\$filter,
		'caps:s{,}' =>           \&process_opts,
		'help' => 		\&usage)) {
	usage();
}




if(!$xml_f || !$ws_server ) {
	usage();
}

sub process_opts($$) {
	my ($n, $v) = @_;
	if($n eq "caps") {
		push(@caps, $v);
	}
}

open(XML_F, "<", "$xml_f.xml") or die "cannot open $xml_f: $@";
#open(BASE64, '-|', "base64", '-e', "$nb_f", '-' ) or die "cannot open pipe: $@";
#while(<BASE64>) {
#	s///;
#	$base64 .= $_;
#}
#close(BASE64) or die "cannot close pipe: $@";

while(<XML_F>) {
	if(/<\?xml/) {
		$content_on = 1;
	}
	#s/PLACEHOLDER\n/$base65/;
	if(defined($filter)) {
		s/PH_FILTER/$filter/;
	}
	else {
		s/^.*PH_FILTER.*$//;
	}
	if(/PH_CAPABILITY_URNS/) {
		my $caps_s = "";
		for(my $i=0; $i<@caps; $i++) {
			$caps_s .= "<capabilityURN>$caps[$i]</capabilityURN>";
		}
		s/PH_CAPABILITY_URNS/$caps_s/;
	}
	if($content_on) {
		$content_out .= $_;
	}
	else {
		$header_out .= $_;
	}
}
close(XML_F) or die "cannot close $xml_f: $@";

my $l = length($content_out);
$header_out =~ s/CONTENT_LENGTH/$l/;

my $xml_out = $header_out.$content_out;
if(defined($dump_req)) {
	print("------ XML Request ------\n");
	print($content_out);
	print("-------------------------\n");
}

my $ws_socket = new IO::Socket::INET(
	PeerAddr=>$ws_server,
	Proto=>'tcp',
	Type=>SOCK_STREAM
);
die "cannot connect to $ws_server: $@" if(!defined($ws_socket));
$ws_socket->syswrite($xml_out);
$ws_socket->shutdown(1);

local $SIG{ALRM} = \&ws_timeout;
alarm 30;

my $ws_response = "";
my $buff = "";

while($ws_socket->sysread($buff, 128)>0) {
	$ws_response .= $buff;
}

alarm 0;
$ws_socket->shutdown(2);

$ws_response =~ s///;
# to take care of chunked http messages
$ws_response =~ s/\r\n[0-9A-Fa-f]+\r\n//gs;
# prettier format
if(defined($prettier)) {
	$ws_response =~ s/></>\n</gs;
}
print $ws_response;

