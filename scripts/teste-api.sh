# Uso: source scripts/teste-api.sh  (a partir de ~/ipam)
USUARIO=$(grep -oP '^IPAM_DEV_USER=\K.*' .env)
SENHA=$(grep -oP '^IPAM_DEV_PASSWORD=\K.*' .env)
B=http://localhost:8080/api

api()  { [ -z "$SENHA" ] && { echo "ERRO: SENHA vazia (rode a partir de ~/ipam)"; return 1; }; curl -s -u "$USUARIO:$SENHA" -H 'Content-Type: application/json' -w '\n[HTTP %{http_code}]\n' "$@"; }
apij() { [ -z "$SENHA" ] && { echo "ERRO: SENHA vazia (rode a partir de ~/ipam)"; return 1; }; curl -s -u "$USUARIO:$SENHA" -H 'Content-Type: application/json' "$@" | jq; }

echo "Funções api/apij carregadas (usuário: $USUARIO)"
