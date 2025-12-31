#!/bin/sh
set -e

STATIC=static
DIST=dist

mkdir -p $DIST

JS_HASH=$(sha1sum $STATIC/js/app.js | awk '{print substr($1,1,8)}')
CSS_HASH=$(sha1sum $STATIC/css/style.css | awk '{print substr($1,1,8)}')

cp $STATIC/js/app.js $DIST/app.$JS_HASH.js
cp $STATIC/css/style.css $DIST/style.$CSS_HASH.css

sed \
  -e "s/{{JS_HASH}}/$JS_HASH/g" \
  -e "s/{{CSS_HASH}}/$CSS_HASH/g" \
  $STATIC/index.html.template > $DIST/index.html

echo "UI build completed"
ls -l $DIST
