#!/bin/bash

# Verificăm dacă s-a dat numele testului
if [ -z "$1" ]; then
    echo "Utilizare: ./debug_diff.sh <nume_test>"
    echo "Exemplu:   ./debug_diff.sh test_small"
    exit 1
fi

TEST_NAME=$1
REF_DIR="output/${TEST_NAME}"
MY_DIR=".."

# Verificăm dacă există directoarele
if [ ! -d "$REF_DIR" ] || [ ! -d "$MY_DIR" ]; then
    echo "Eroare: Nu găsesc directoarele de output."
    echo "Asigură-te că ai rulat './checker.sh ${TEST_NAME}' înainte!"
    exit 1
fi

echo "=========================================================="
echo "COMPARARE DETALIATĂ PENTRU: ${TEST_NAME}"
echo "Referință: ${REF_DIR}"
echo "Soluția ta: ${MY_DIR}"
echo "=========================================================="

# Funcție pentru a compara un fișier specific
compare_file() {
    FILE_NAME=$1
    echo ""
    echo ">>> Analiză fișier: ${FILE_NAME}"
    
    if [ ! -f "${REF_DIR}/${FILE_NAME}" ]; then
        echo "   [!] Fișierul ${FILE_NAME} lipsește din referință!"
        return
    fi
    if [ ! -f "${MY_DIR}/${FILE_NAME}" ]; then
        echo "   [!] Fișierul ${FILE_NAME} lipsește din soluția ta!"
        return
    fi

    # 1. Diff normal (arată liniile diferite)
    DIFF_OUT=$(diff -u --color=always "${REF_DIR}/${FILE_NAME}" "${MY_DIR}/${FILE_NAME}")
    
    if [ -z "$DIFF_OUT" ]; then
        echo "   OK! Fișierele sunt identice."
    else
        echo "   [X] DIFERENȚE GĂSITE:"
        echo "$DIFF_OUT"
        
        echo ""
        echo "   [i] Verificare caractere invizibile (Referință vs Tu):"
        echo "   (Dolar '$' indică sfârșitul liniei. Verifică spații înainte de $)"
        echo "   -----------------------------------------------------"
        echo "   REFERINȚĂ:"
        cat -e "${REF_DIR}/${FILE_NAME}" | head -n 5
        echo "   ..."
        echo "   SOLUȚIA TA:"
        cat -e "${MY_DIR}/${FILE_NAME}" | head -n 5
    fi
}

# Comparăm fișierele principale
compare_file "reports.txt"
compare_file "all_articles.txt"
compare_file "keywords_count.txt"

echo ""
echo "=========================================================="