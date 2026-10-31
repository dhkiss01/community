FROM node:24-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev --ignore-scripts
COPY app.js ./
COPY html ./html
COPY css ./css
COPY js ./js
COPY public ./public
EXPOSE 3000
USER node
CMD ["node", "app.js"]
