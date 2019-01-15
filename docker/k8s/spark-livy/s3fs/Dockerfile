#
# Copyright © 2016-2018 The Thingsboard Authors
# Modifications © 2017-2018 Hashmap, Inc
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM alpine:3.7

# the following ENV need to be present
ENV IAM_ROLE=none
ENV MOUNT_POINT=/var/s3
VOLUME /var/s3

ARG S3FS_VERSION=v1.79

RUN apk --update add fuse-dev alpine-sdk automake curl-dev php5 autoconf openssl-dev libxml2-dev libcrypto1.0 libcurl git bash

RUN git clone https://github.com/s3fs-fuse/s3fs-fuse.git && \
cd s3fs-fuse && \
git checkout tags/${S3FS_VERSION} && \
./autogen.sh && \
./configure --prefix=/usr && \
make && \
make install && \
rm -rf /var/cache/apk/*

COPY entrypoint.sh /
ENTRYPOINT ["/entrypoint.sh"]
CMD ["php5", "-S", "0.0.0.0:80","-t","/var/s3"]
