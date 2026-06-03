# 🚀 Deployment Guide — Architect Ledger

This guide provides step-by-step instructions to deploy the **Architect Ledger** full-stack application for free using **Vercel** (for the frontend) and **Render** (for the backend).

---

## 🛠️ Prerequisites
1. A **GitHub** account.
2. A **Vercel** account (connected to GitHub).
3. A **Render** account (connected to GitHub).
4. *(Optional)* A **Neon.tech** account (if you want a free persistent PostgreSQL database).

---

## 📂 Step 1: Push Code to GitHub

Before deploying, ensure all local changes (including the new `vercel.json` file) are committed and pushed to a private or public GitHub repository.

1. Initialize git (if not already done) and commit the changes:
   ```bash
   git add .
   git commit -m "Configure deployment and Vercel routing"
   ```
2. Create a new repository on GitHub and link it locally:
   ```bash
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
   git branch -M main
   git push -u origin main
   ```

---

## 💾 Step 2: Database Setup (Optional but Recommended)

By default, the backend uses an **in-memory H2 database**. Since Render's free tier spins down on inactivity, your data will reset whenever the backend restarts. 
To keep your data persistent, set up a free PostgreSQL database.

### Option A: Serverless PostgreSQL on Neon (Recommended)
1. Go to [Neon.tech](https://neon.tech/) and sign up for a free account.
2. Create a new project. Select **PostgreSQL** (version 15 or 16 is fine).
3. In the Neon dashboard, copy your **Connection String** (choose `Java / JDBC` or use standard URI string and convert it).
   - If using the raw connection URI (looks like `postgresql://user:password@host/dbname?sslmode=require`), map it as follows:
     - **DB_URL**: `jdbc:postgresql://host/dbname?sslmode=require`
     - **DB_USER**: the user portion
     - **DB_PASS**: the password portion

### Option B: PostgreSQL on Render
1. In your **Render Dashboard**, click **New +** and select **PostgreSQL**.
2. Name your database, leave it on the **Free** tier, and click **Create Database**.
3. Once created, copy the **External Database URL**.
   - Convert it to JDBC format: Replace `postgres://` with `jdbc:postgresql://`.
   - **DB_URL**: `jdbc:postgresql://<host>:<port>/<dbname>`
   - **DB_USER**: copy the Database User value from Render.
   - **DB_PASS**: copy the Database Password value from Render.

---

## ⚙️ Step 3: Deploy the Backend on Render

Render will build and run the Spring Boot backend inside a Docker container using the provided `Dockerfile`.

1. Go to your **Render Dashboard**, click **New +** and select **Web Service**.
2. Connect your GitHub repository.
3. Configure the Web Service:
   - **Name**: `architect-ledger-backend`
   - **Region**: Select the region closest to you.
   - **Branch**: `main`
   - **Root Directory**: `backend` *(This is important!)*
   - **Runtime**: `Docker` *(Render should auto-detect this because of the Dockerfile in the root/backend directory)*
   - **Instance Type**: **Free**
4. Expand the **Advanced** section to add the following **Environment Variables**:

| Key | Value | Description |
| :--- | :--- | :--- |
| `ALLOWED_ORIGINS` | `https://your-frontend.vercel.app` | *Update this after Step 4 with your Vercel URL, or set to `*` temporarily* |
| `JWT_SECRET` | *(Generate a random 64-character hex string)* | Used to sign security tokens. |
| `DB_URL` | *`jdbc:postgresql://...` (Optional)* | Only if using Neon/Render PostgreSQL database. |
| `DB_USER` | *Your DB Username (Optional)* | Only if using Neon/Render PostgreSQL database. |
| `DB_PASS` | *Your DB Password (Optional)* | Only if using Neon/Render PostgreSQL database. |
| `DB_DDL_AUTO` | `update` *(Optional)* | Only if using PostgreSQL; allows Hibernate to auto-create tables. |

5. Click **Deploy Web Service**.
   > [!NOTE]
   > The first build might take 3-5 minutes as it downloads Maven dependencies and compiles the Java app. Once complete, you will see a URL (e.g., `https://architect-ledger-backend.onrender.com`). Copy this URL!

---

## 🎨 Step 4: Deploy the Frontend on Vercel

Vercel will host the React app and automatically apply SPA router rewrites via `vercel.json`.

1. Go to [Vercel](https://vercel.com/) and click **Add New > Project**.
2. Import your GitHub repository.
3. Configure the Project:
   - **Framework Preset**: Select **Vite**.
   - **Root Directory**: Click Edit, select the `frontend` folder, and click **Continue**.
4. Expand **Environment Variables** and add:
   - **Name**: `VITE_API_BASE_URL`
   - **Value**: Your Render Backend URL (e.g., `https://architect-ledger-backend.onrender.com`)
     > [!IMPORTANT]
     > Ensure there is **no trailing slash** at the end of the URL (e.g., use `https://architect-ledger-backend.onrender.com` instead of `https://architect-ledger-backend.onrender.com/`).
5. Click **Deploy**.
   > [!TIP]
   > Vercel will build and deploy the React app in less than a minute. You will get a production URL (e.g., `https://your-project.vercel.app`).

---

## 🔄 Step 5: Update CORS on Backend (Important!)

To allow your React frontend to securely talk to the backend, you must configure CORS on the backend.

1. Copy your Vercel deployment URL (e.g., `https://architect-ledger-abc.vercel.app`).
2. Go to your backend web service on **Render**.
3. Navigate to the **Environment** tab.
4. Update the `ALLOWED_ORIGINS` variable value to your Vercel URL.
5. Save changes. Render will automatically redeploy the backend with the new origin restriction.

---

## ✅ Step 6: Verify Deployment
1. Open your Vercel URL in a browser.
2. Sign up a new user or log in.
3. Create a transaction and check if the dashboard updates.
4. Verify the logs in Render's dashboard if you encounter any communication errors.
