###########################################################
#
# Dockerfile for Dockerhub-webhook
#
###########################################################

# Setting the base to nodejs 7
FROM mhart/alpine-node:7

# Maintainer
MAINTAINER Jonas Enge

#### Begin setup ####

# Installs docker
RUN apk add --update --no-cache docker py-pip
RUN apk add bash bash-doc bash-completion
RUN pip install docker-compose

# Extra tools for native dependencies
# RUN apk add --no-cache make gcc g++ python

# Bundle app source
COPY . /src

# Change working directory
WORKDIR "/src"

# Install dependencies
RUN npm install --production

# Env variables
ENV SERVER_PORT 3000
ENV API_ROUTE /api
ENV TOKEN abc123

# Expose 3000
EXPOSE 3000

# Startup
ENTRYPOINT node index.js
