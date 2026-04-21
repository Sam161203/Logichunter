# LogicHunter (Academic Version)

LogicHunter is a Burp Suite extension prototype designed to assist in structured analysis of application traffic during security testing.

## ⚠️ Notice

This repository contains a **limited academic version** of the tool.

Core analysis logic, detection engines, and advanced processing components have been intentionally removed.

## 🎯 Purpose

The project demonstrates:

* Capturing and organizing HTTP request/response data
* Structuring observations for manual security testing
* Building a modular extension using the Burp Montoya API

## 🧩 Features (Academic Version)

* Basic traffic capture and logging
* Simple data modeling for requests and responses
* UI tab integration inside Burp Suite
* Modular project structure for extension development

## 🚫 Limitations

This version does NOT include:

* Automated vulnerability detection
* Heuristic or scoring systems
* Correlation or analysis engines
* AI/LLM integrations

## 🛠️ Tech Stack

* Java
* Burp Suite Montoya API
* Gradle

## 🧪 Usage

1. Build the extension:

   ```
   gradlew jar
   ```
2. Load into Burp Suite:

   * Extensions → Add → Select JAR file

## 📌 Disclaimer

This project is shared for **educational and demonstration purposes only**.

It does not represent the full internal capabilities of the original tool.

## 👤 Author

Sam – Security Researcher
