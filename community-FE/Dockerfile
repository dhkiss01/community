FROM node:22

WORKDIR /app

ENV NODE_ENV=production

COPY package*.json ./
RUN npm ci --omit=dev

COPY . .

EXPOSE 8082

CMD ["node", "app.js"]

WORKDIR /app


COPY package.json package-lock.json ./
RUN npm ci --only=production --silent


COPY . .


EXPOSE 3000


CMD ["npm", "start"]
