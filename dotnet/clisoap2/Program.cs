// dotnet run
// http://localhost:5000/?n=10
using System.Xml.Linq;
using System.Text.Json;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapGet("/", async (int n) =>
{
    const string endpoint =
        "https://www.dataaccess.com/webservicesserver/NumberConversion.wso";

    var envelope = $"""
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
              <ubiNum>{n}</ubiNum>
            </NumberToWords>
          </soap:Body>
        </soap:Envelope>
        """;

    using var http = new HttpClient();
    var content = new StringContent(envelope, System.Text.Encoding.UTF8, "text/xml");
    var soapResp = await http.PostAsync(endpoint, content);
    var xml = XDocument.Parse(await soapResp.Content.ReadAsStringAsync());
    XNamespace ns = "http://www.dataaccess.com/webservicesserver/";
    var word = xml.Descendants(ns + "NumberToWordsResult").First().Value.Trim();

    var translateUrl = "https://translate.googleapis.com/translate_a/single" +
        $"?client=gtx&sl=en&tl=es&dt=t&q={Uri.EscapeDataString(word)}";
    var json = await http.GetStringAsync(translateUrl);
    var doc = JsonDocument.Parse(json);
    var translated = doc.RootElement[0][0][0].GetString() ?? "";
    return Results.Text(translated, "text/plain", System.Text.Encoding.UTF8);
});

app.Run("http://localhost:5000");
