# Deployment Guide - PKMN/OP Binder

This guide explains how to deploy the frontend to Vercel and the backend to Render, making the application publicly accessible.

## Architecture

- **Frontend**: Deployed on Vercel (Static hosting with CDN)
- **Backend**: Deployed on Render (Docker containers)
- **Databases**: PostgreSQL on Render
- **Message Queue**: CloudAMQP (RabbitMQ as a service)
- **Redis**: Render Redis

---

## Prerequisites

1. GitHub account
2. Vercel account (free): https://vercel.com
3. Render account (free): https://render.com
4. CloudAMQP account (free): https://www.cloudamqp.com

---

## Step 1: Push Code to GitHub

```bash
# Initialize git repository (if not already done)
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit - PKMN/OP Binder"

# Create a new repository on GitHub, then:
git remote add origin https://github.com/YOUR_USERNAME/pkmn-op-binder.git
git branch -M main
git push -u origin main
```

---

## Step 2: Set Up CloudAMQP (RabbitMQ)

1. Go to https://www.cloudamqp.com and sign up
2. Create a new instance:
   - Plan: **Little Lemur (Free)**
   - Name: `pkmn-rabbitmq`
   - Region: Choose closest to you
3. Once created, note down:
   - **AMQP URL** (will look like: `amqps://username:password@host/vhost`)
   - Extract: hostname, username, password

---

## Step 3: Deploy Backend to Render

### Option A: Using render.yaml (Blueprint)

1. Go to https://dashboard.render.com
2. Click **"New +"** → **"Blueprint"**
3. Connect your GitHub repository
4. Render will detect `render.yaml` automatically
5. **Update environment variables**:
   - Add your CloudAMQP credentials:
     - `RABBITMQ_HOST`: From CloudAMQP dashboard
     - `RABBITMQ_USER`: From CloudAMQP dashboard
     - `RABBITMQ_PASS`: From CloudAMQP dashboard
6. Click **"Apply"** to deploy all services

### Option B: Manual Setup

#### 3.1 Create Databases

1. Go to Render Dashboard → **"New +"** → **"PostgreSQL"**
2. Create two databases:
   - Name: `catalog-db`, Database: `catalog_db`
   - Name: `collection-db`, Database: `collection_db`
3. Both on **Free** plan

#### 3.2 Create Redis

1. **"New +"** → **"Redis"**
2. Name: `redis`
3. Plan: **Free**

#### 3.3 Deploy API Gateway

1. **"New +"** → **"Web Service"**
2. Connect GitHub repository
3. Settings:
   - **Name**: `api-gateway`
   - **Runtime**: Docker
   - **Dockerfile Path**: `./api-gateway/Dockerfile`
   - **Docker Context**: `.` (root)
   - **Plan**: Free
4. Environment Variables:
   ```
   REDIS_HOST=<from Render Redis>
   REDIS_PORT=<from Render Redis>
   RABBITMQ_HOST=<from CloudAMQP>
   RABBITMQ_PORT=5672
   RABBITMQ_USER=<from CloudAMQP>
   RABBITMQ_PASS=<from CloudAMQP>
   ```
5. Health Check Path: `/actuator/health`
6. Click **"Create Web Service"**

#### 3.4 Deploy Catalog Service

1. **"New +"** → **"Web Service"**
2. Settings:
   - **Name**: `catalog-service`
   - **Runtime**: Docker
   - **Dockerfile Path**: `./catalog-service/Dockerfile`
   - **Docker Context**: `./catalog-service`
3. Environment Variables:
   ```
   DB_HOST=<from catalog-db>
   DB_PORT=5432
   DB_NAME=catalog_db
   DB_USER=<from catalog-db>
   DB_PASSWORD=<from catalog-db>
   RABBITMQ_HOST=<from CloudAMQP>
   RABBITMQ_PORT=5672
   RABBITMQ_USER=<from CloudAMQP>
   RABBITMQ_PASS=<from CloudAMQP>
   ```
4. Health Check Path: `/actuator/health`

#### 3.5 Deploy Collection Service

1. **"New +"** → **"Web Service"**
2. Settings:
   - **Name**: `collection-service`
   - **Runtime**: Docker
   - **Dockerfile Path**: `./collection-service/Dockerfile`
   - **Docker Context**: `.` (root)
3. Environment Variables:
   ```
   DB_HOST=<from collection-db>
   DB_PORT=5432
   DB_NAME=collection_db
   DB_USER=<from collection-db>
   DB_PASSWORD=<from collection-db>
   RABBITMQ_HOST=<from CloudAMQP>
   RABBITMQ_PORT=5672
   RABBITMQ_USER=<from CloudAMQP>
   RABBITMQ_PASS=<from CloudAMQP>
   ```
4. Health Check Path: `/actuator/health`

#### 3.6 Deploy Media Service

1. **"New +"** → **"Web Service"**
2. Settings:
   - **Name**: `media-service`
   - **Runtime**: Docker
   - **Dockerfile Path**: `./media-service/Dockerfile`
   - **Docker Context**: `./media-service`
3. Health Check Path: `/health`

### Note Your Service URLs

After deployment, note down the URLs:
- API Gateway: `https://api-gateway-XXXX.onrender.com`
- Media Service: `https://media-service-XXXX.onrender.com`

---

## Step 4: Update Frontend Configuration

1. Open `frontend/app.js`
2. Update lines 5 and 10 with your Render URLs:

```javascript
const API_GATEWAY_URL = isProduction
    ? 'https://api-gateway-XXXX.onrender.com'  // Your actual Render URL
    : 'http://localhost:8080';

const MEDIA_BASE_URL = isProduction
    ? 'https://media-service-XXXX.onrender.com'  // Your actual Render URL
    : 'http://localhost:8084';
```

3. Commit and push changes:
```bash
git add frontend/app.js
git commit -m "Update production API URLs"
git push
```

---

## Step 5: Enable CORS on Backend

Update your API Gateway to allow CORS from your Vercel domain.

In `api-gateway/src/main/resources/application.yml`, add:
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "https://your-app.vercel.app"
              - "http://localhost:3001"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
```

Commit and push - Render will auto-deploy.

---

## Step 6: Deploy Frontend to Vercel

### Method 1: Vercel Dashboard (Easiest)

1. Go to https://vercel.com/dashboard
2. Click **"Add New..."** → **"Project"**
3. Import your GitHub repository
4. Settings:
   - **Framework Preset**: Other
   - **Root Directory**: `frontend`
   - **Build Command**: Leave empty (static site)
   - **Output Directory**: Leave empty
5. Click **"Deploy"**

### Method 2: Vercel CLI

```bash
# Install Vercel CLI
npm install -g vercel

# Navigate to frontend directory
cd frontend

# Deploy
vercel

# Follow prompts:
# - Link to existing project? No
# - Project name? pkmn-tcg-binder
# - Directory? ./
# - Override settings? No

# Deploy to production
vercel --prod
```

---

## Step 7: Test Your Deployment

1. Open your Vercel URL: `https://your-app.vercel.app`
2. Test features:
   - Browse catalog
   - Add cards to collection
   - View collection
   - Check progress
   - Toggle dark mode

---

## Troubleshooting

### Backend Services Won't Start
- **Check logs** in Render dashboard
- Verify all environment variables are set
- Check database connections
- Ensure Dockerfiles build successfully locally

### Frontend Can't Connect to Backend
- Verify API URLs in `frontend/app.js` are correct
- Check CORS configuration in API Gateway
- Open browser console for error messages
- Test API directly: `https://your-api-gateway.onrender.com/actuator/health`

### RabbitMQ Connection Issues
- Verify CloudAMQP credentials
- Check if CloudAMQP instance is running
- Test connection from Render services

### Free Tier Limitations
- **Render Free**: Services sleep after 15 min inactivity (cold starts take ~30 seconds)
- **Vercel Free**: Unlimited bandwidth, 100 GB-hours serverless function execution
- **CloudAMQP Free**: 1 million messages/month

---

## Cost Estimate

- **Vercel**: Free forever for this use case
- **Render**: Free tier (with cold starts)
- **CloudAMQP**: Free tier (sufficient for demo)

**Total: $0/month** ✅

---

## Making It Production-Ready

For a real production deployment:

1. **Upgrade to paid tiers** (no cold starts)
2. **Add custom domain** (Vercel makes this easy)
3. **Enable authentication** (implement OAuth2)
4. **Add monitoring** (Sentry, LogRocket)
5. **Set up CI/CD** (GitHub Actions)
6. **Add backup strategy** for databases
7. **Implement rate limiting** properly

---

## Security Notes

⚠️ **Important for Exam Submission:**

- This setup is for **demonstration purposes**
- No authentication is currently enforced
- Database credentials are in environment variables (good)
- Never commit secrets to git
- For production, use secrets management (Vault, AWS Secrets Manager)

---

## Support

If you encounter issues:
- Check Render logs: Dashboard → Service → Logs
- Check Vercel logs: Dashboard → Project → Deployments → View Function Logs
- Test locally first with Docker Compose
- Ensure all services are healthy before testing frontend

---

**Good luck with your deployment! 🚀**
