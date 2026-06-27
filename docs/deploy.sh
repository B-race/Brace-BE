#!/bin/bash

cat <<'EOF' >/home/ec2-user/deploy.sh
#!/bin/bash
set -e

AWS_REGION=ap-northeast-2
ACCOUNT_ID=813875215068
REPOSITORY=brace-mesh
IMAGE_TAG="${1:?image tag is required}"

decode_env() {
  printf '%s' "$1" | base64 -d
}

DB_URL="$(decode_env "${DB_URL_B64:?DB_URL_B64 is required}")"
DB_USER="$(decode_env "${DB_USER_B64:?DB_USER_B64 is required}")"
DB_PW="$(decode_env "${DB_PW_B64:?DB_PW_B64 is required}")"
GOOGLE_CLIENT_ID="$(decode_env "${GOOGLE_CLIENT_ID_B64:-}")"
NAVER_CLIENT_ID="$(decode_env "${NAVER_CLIENT_ID_B64:-}")"
NAVER_CLIENT_SECRET="$(decode_env "${NAVER_CLIENT_SECRET_B64:-}")"
NAVER_REDIRECT_URI="$(decode_env "${NAVER_REDIRECT_URI_B64:-}")"

IMAGE=$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPOSITORY:$IMAGE_TAG

aws ecr get-login-password --region $AWS_REGION \
| docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

docker pull $IMAGE

docker stop app || true
docker rm app || true

docker run -d \
  --name app \
  --restart unless-stopped \
  -p 8080:8080 \
  -e DB_URL="$DB_URL" \
  -e DB_USER="$DB_USER" \
  -e DB_PW="$DB_PW" \
  -e GOOGLE_CLIENT_ID="$GOOGLE_CLIENT_ID" \
  -e NAVER_CLIENT_ID="$NAVER_CLIENT_ID" \
  -e NAVER_CLIENT_SECRET="$NAVER_CLIENT_SECRET" \
  -e NAVER_REDIRECT_URI="$NAVER_REDIRECT_URI" \
  $IMAGE
EOF

chmod +x /home/ec2-user/deploy.sh
