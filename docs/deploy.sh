#!/bin/bash

cat <<'EOF' >/home/ec2-user/deploy.sh
#!/bin/bash
set -e

AWS_REGION=ap-northeast-2
ACCOUNT_ID=813875215068
REPOSITORY=brace-mesh
IMAGE_TAG="${1:?image tag is required}"

get_required_parameter() {
  aws ssm get-parameter \
    --name "$1" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text \
    --region "$AWS_REGION"
}

get_optional_parameter() {
  aws ssm get-parameter \
    --name "$1" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text \
    --region "$AWS_REGION" 2>/dev/null || true
}

DB_URL="$(get_required_parameter /brace/prod/DB_URL)"
DB_USER="$(get_required_parameter /brace/prod/DB_USER)"
DB_PW="$(get_required_parameter /brace/prod/DB_PW)"
CORS_ALLOWED_ORIGINS="$(get_required_parameter /brace/prod/CORS_ALLOWED_ORIGINS)"
GOOGLE_CLIENT_ID="$(get_optional_parameter /brace/prod/GOOGLE_CLIENT_ID)"
NAVER_CLIENT_ID="$(get_optional_parameter /brace/prod/NAVER_CLIENT_ID)"
NAVER_CLIENT_SECRET="$(get_optional_parameter /brace/prod/NAVER_CLIENT_SECRET)"
NAVER_REDIRECT_URI="$(get_optional_parameter /brace/prod/NAVER_REDIRECT_URI)"

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
  -e CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS" \
  -e GOOGLE_CLIENT_ID="$GOOGLE_CLIENT_ID" \
  -e NAVER_CLIENT_ID="$NAVER_CLIENT_ID" \
  -e NAVER_CLIENT_SECRET="$NAVER_CLIENT_SECRET" \
  -e NAVER_REDIRECT_URI="$NAVER_REDIRECT_URI" \
  $IMAGE
EOF

chmod +x /home/ec2-user/deploy.sh
