FROM nginx:alpine

WORKDIR /ui

COPY static ./static
COPY build.sh .

RUN apk add --no-cache bash coreutils \
    && chmod +x build.sh \
    && ./build.sh

# Replace default nginx content
RUN rm -rf /usr/share/nginx/html/*
RUN cp -r dist/* /usr/share/nginx/html/

CMD ["nginx", "-g", "daemon off;"]
