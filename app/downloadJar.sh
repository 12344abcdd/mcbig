mkdir -p mc_jars && cd mc_jars

# 1. 下载主客户端jar
#jq -r '.downloads.client.url' ../version.json | xargs -I {} wget -c {}

# 2. 下载所有依赖库jar
jq -r '.libraries[].downloads.artifact.url | select(.)' ../version.json | xargs -L1 wget -c