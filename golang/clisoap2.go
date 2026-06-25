// go run clisoap2.go
// http://localhost:8080/?n=10
package main

import (
	"encoding/json"
	"encoding/xml"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
)

type SoapEnvelope struct {
	Body SoapBody `xml:"Body"`
}
type SoapBody struct {
	Response NumberToWordsResponse `xml:"NumberToWordsResponse"`
}
type NumberToWordsResponse struct {
	Result string `xml:"NumberToWordsResult"`
}

func translateToES(text string) string {
	apiURL := "https://translate.googleapis.com/translate_a/single" +
		"?client=gtx&sl=en&tl=es&dt=t&q=" + url.QueryEscape(text)
	resp, _ := http.Get(apiURL)
	defer resp.Body.Close()
	data, _ := io.ReadAll(resp.Body)
	var result [][][]any
	json.Unmarshal(data, &result)
	return result[0][0][0].(string)
}

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		n := r.URL.Query().Get("n")
		if n == "" {
			n = "10"
		}
		payload := fmt.Sprintf(`<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
      <ubiNum>%s</ubiNum>
    </NumberToWords>
  </soap:Body>
</soap:Envelope>`, n)

		resp, _ := http.Post(
			"https://www.dataaccess.com/webservicesserver/NumberConversion.wso",
			"text/xml; charset=utf-8",
			strings.NewReader(payload),
		)
		defer resp.Body.Close()
		data, _ := io.ReadAll(resp.Body)
		var env SoapEnvelope
		xml.Unmarshal(data, &env)
		word := strings.TrimSpace(env.Body.Response.Result)
		fmt.Fprint(w, translateToES(word))
	})
	fmt.Println("http://localhost:8080/?n=10")
	http.ListenAndServe(":8080", nil)
}
