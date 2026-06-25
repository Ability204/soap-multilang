# ruby conintl.rb
# http://localhost:4567/?n=10
require 'webrick'

ONES = %w[cero uno dos tres cuatro cinco seis siete ocho nueve diez
          once doce trece catorce quince dieciséis diecisiete dieciocho diecinueve]
TENS = ['', '', 'veinte', 'treinta', 'cuarenta', 'cincuenta',
        'sesenta', 'setenta', 'ochenta', 'noventa']
HUNDREDS = ['', 'ciento', 'doscientos', 'trescientos', 'cuatrocientos',
            'quinientos', 'seiscientos', 'setecientos', 'ochocientos', 'novecientos']

def to_words_es(n)
  n = n.to_i
  return 'cero'   if n == 0
  return ONES[n]  if n < 20
  if n < 100
    return TENS[n / 10] if n % 10 == 0
    return "#{TENS[n / 10]} y #{ONES[n % 10]}"
  end
  if n < 1000
    return 'cien' if n == 100
    rest = to_words_es(n % 100)
    return rest.empty? ? HUNDREDS[n / 100] : "#{HUNDREDS[n / 100]} #{rest}"
  end
  n.to_s
end

server = WEBrick::HTTPServer.new(Port: 4567, Logger: WEBrick::Log.new('/dev/null'), AccessLog: [])

server.mount_proc '/' do |req, res|
  n = (req.query['n'] || '10').to_i
  res.body = to_words_es(n)
end

trap('INT') { server.shutdown }
server.start
