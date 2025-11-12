# Deployment Checklist

Use this checklist to deploy your application to production.

## Pre-Deployment

- [ ] Code is pushed to GitHub
- [ ] All tests pass locally
- [ ] Docker images build successfully
- [ ] Application runs correctly with `docker compose up`

## CloudAMQP Setup (RabbitMQ)

- [ ] CloudAMQP account created
- [ ] Free "Little Lemur" instance created
- [ ] Note down credentials:
  - [ ] Hostname: `_________________`
  - [ ] Username: `_________________`
  - [ ] Password: `_________________`

## Render Backend Deployment

- [ ] Render account created
- [ ] PostgreSQL databases created:
  - [ ] `catalog-db` created
  - [ ] `collection-db` created
- [ ] Redis instance created
- [ ] Services deployed:
  - [ ] API Gateway deployed
  - [ ] Catalog Service deployed
  - [ ] Collection Service deployed
  - [ ] Media Service deployed
- [ ] All environment variables configured
- [ ] All services showing "Live" status
- [ ] Health checks passing

### Note Service URLs

- API Gateway URL: `https://_____________________.onrender.com`
- Media Service URL: `https://_____________________.onrender.com`

## Frontend Configuration

- [ ] Updated `frontend/app.js` with production URLs
- [ ] Tested CORS configuration
- [ ] Changes committed and pushed to GitHub

## Vercel Frontend Deployment

- [ ] Vercel account created
- [ ] GitHub repository connected
- [ ] Project deployed from `frontend/` directory
- [ ] Deployment successful
- [ ] Site is live

### Frontend URL

- Vercel URL: `https://_____________________.vercel.app`

## Testing

- [ ] Frontend loads successfully
- [ ] Can view catalog
- [ ] Can add cards to collection
- [ ] Can view collection
- [ ] Can delete cards from collection
- [ ] Progress tracking works
- [ ] Dark mode toggle works
- [ ] Search and filters work

## Post-Deployment

- [ ] Share URLs with team/examiner
- [ ] Document any issues encountered
- [ ] Add deployment URLs to README.md

## Troubleshooting

If something doesn't work:

1. **Check Render logs** for each service
2. **Check browser console** for frontend errors
3. **Test API endpoints directly** using browser or Postman
4. **Verify CORS** is configured correctly
5. **Check environment variables** are set

## Free Tier Limitations

⚠️ Remember:
- Render free services **sleep after 15 minutes** of inactivity
- First request after sleep takes **30-50 seconds** (cold start)
- This is normal for free tier!

## Optional Enhancements

- [ ] Add custom domain to Vercel
- [ ] Set up monitoring (Sentry)
- [ ] Configure custom CORS domains
- [ ] Add GitHub Actions for CI/CD
- [ ] Set up database backups

---

**Deployment Date**: ___________

**Status**: ✅ Deployed  |  ⏳ In Progress  |  ❌ Issues

**Notes**:
