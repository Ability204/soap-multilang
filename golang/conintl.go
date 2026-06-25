// go run conintl.go
// http://localhost:8080/?n=10
package main

import (
	"fmt"
	"net/http"
	"strconv"
)

var ones = []string{
	"", "uno", "dos", "tres", "cuatro", "cinco",
	"seis", "siete", "ocho", "nueve", "diez",
	"once", "doce", "trece", "catorce", "quince",
	"dieciséis", "diecisiete", "dieciocho", "diecinueve",
}
var tensWords = []string{
	"", "", "veinte", "treinta", "cuarenta", "cincuenta",
	"sesenta", "setenta", "ochenta", "noventa",
}
var hundreds = []string{
	"", "ciento", "doscientos", "trescientos", "cuatrocientos",
	"quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos",
}

func toWordsES(n int) string {
	switch {
	case n == 0:
		return "cero"
	case n < 20:
		return ones[n]
	case n < 100:
		if n%10 == 0 {
			return tensWords[n/10]
		}
		return tensWords[n/10] + " y " + ones[n%10]
	case n < 1000:
		if n == 100 {
			return "cien"
		}
		rest := toWordsES(n % 100)
		if rest == "" {
			return hundreds[n/100]
		}
		return hundreds[n/100] + " " + rest
	default:
		return strconv.Itoa(n)
	}
}

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		n, _ := strconv.Atoi(r.URL.Query().Get("n"))
		fmt.Fprint(w, toWordsES(n))
	})
	fmt.Println("http://localhost:8080/?n=10")
	http.ListenAndServe(":8080", nil)
}
