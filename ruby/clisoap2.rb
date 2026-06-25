# gem install savon webrick
# ruby clisoap2.rb
# http://localhost:4567/?n=10
require 'webrick'
require 'savon'
require 'net/http'
require 'uri'
require 'json'

def translate_to_es(text)
  uri = URI("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q=#{URI.encode_www_form_component(text)}")
  JSON.parse(Net::HTTP.get(uri))[0][0][0]
end

server = WEBrick::HTTPServer.new(Port: 4567, Logger: WEBrick::Log.new('/dev/null'), AccessLog: [])

server.mount_proc '/' do |req, res|
  n = req.query['n'] || '10'
  client = Savon.client(
    wsdl: 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL',
    log: false
  )
  response = client.call(:number_to_words, message: { ubi_num: n })
  word = response.body[:number_to_words_response][:number_to_words_result].to_s.strip
  res.body = translate_to_es(word)
end

trap('INT') { server.shutdown }
server.start
