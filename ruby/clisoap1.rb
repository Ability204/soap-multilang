# gem install savon webrick
# ruby clisoap1.rb
# http://localhost:4567/?n=10
require 'webrick'
require 'savon'

server = WEBrick::HTTPServer.new(Port: 4567, Logger: WEBrick::Log.new('/dev/null'), AccessLog: [])

server.mount_proc '/' do |req, res|
  n = req.query['n'] || '10'
  client = Savon.client(
    wsdl: 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL',
    log: false
  )
  response = client.call(:number_to_words, message: { ubi_num: n })
  result = response.body[:number_to_words_response][:number_to_words_result]
  res.body = result.to_s.strip
end

trap('INT') { server.shutdown }
server.start
