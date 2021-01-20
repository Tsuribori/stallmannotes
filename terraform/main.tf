provider "google" {
  project = var.project_name
  region = "us-central1"
}

resource "google_container_registry" "registry" {}

resource "google_project_service" "run" {
  service = "run.googleapis.com"
}

resource "google_project_service" "scheduler" {
  service = "cloudscheduler.googleapis.com"
}


resource "google_cloud_run_service" "stallman-srv" {
  name     = "stallman-srv"
  location = "us-central1"

  template {
    spec {
      containers {
        image = "gcr.io/${var.project_name}/stallmannotes:latest"
        env {
            name = "CONSUMER_KEY"
            value = var.consumer_key
        }
        env {
            name = "CONSUMER_SECRET"
            value = var.consumer_secret
        }
        env {
            name = "ACCESS_TOKEN"
            value = var.access_token
        }
        env {
            name = "ACCESS_TOKEN_SECRET"
            value = var.access_token_secret
        }
        ports {
            container_port = 8080
        }
      }
    }
  }

  traffic {
    percent         = 100
    latest_revision = true
  }

  depends_on = [google_project_service.run]
 
 }

resource "google_service_account" "service_account" {
  account_id   = "stallman-invoker"
  display_name = "Service Account needed by Cloud Scheduler for triggering stallmanbot"
}

resource "google_cloud_run_service_iam_member" "invoker" {
  location = google_cloud_run_service.stallman-srv.location
  project = google_cloud_run_service.stallman-srv.project
  service = google_cloud_run_service.stallman-srv.name
  role = "roles/run.invoker"
  member = "serviceAccount:${google_service_account.service_account.email}"
}

resource "google_app_engine_application" "app" {
  location_id = "us-central"
}

resource "google_cloud_scheduler_job" "stallman-trigger" {
  name             = "stallman-trigger"
  description      = "Run program once a day"
  schedule         = "0 2 * * *"
  time_zone        = "UTC"
  attempt_deadline = "320s"

  http_target {
    http_method = "GET"
      uri = google_cloud_run_service.stallman-srv.status[0].url
      oidc_token {
      service_account_email = google_service_account.service_account.email
    }
  }

  depends_on = [google_project_service.scheduler, google_cloud_run_service.stallman-srv]
}