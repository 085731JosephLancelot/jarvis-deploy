# jarvis-deploy

A lightweight CLI for managing multi-environment deployments with rollback support and audit logging.

---

## Installation

```bash
git clone https://github.com/your-org/jarvis-deploy.git
cd jarvis-deploy && ./mvnw clean install -DskipTests
```

---

## Usage

```bash
# Deploy to a target environment
java -jar jarvis-deploy.jar deploy --env production --version 2.4.1

# Roll back to the previous release
java -jar jarvis-deploy.jar rollback --env production

# View the audit log for an environment
java -jar jarvis-deploy.jar audit --env staging --limit 20
```

### Common Options

| Flag | Description |
|------|-------------|
| `--env` | Target environment (`dev`, `staging`, `production`) |
| `--version` | Artifact version to deploy |
| `--dry-run` | Simulate the deployment without applying changes |
| `--verbose` | Enable detailed output |

---

## Requirements

- Java 17+
- Maven 3.8+

---

## Configuration

Place a `jarvis.yml` file in your project root to define environment targets, artifact sources, and notification hooks. See [`jarvis.example.yml`](jarvis.example.yml) for a full reference.

---

## Contributing

Pull requests are welcome. Please open an issue first to discuss any significant changes.

---

## License

This project is licensed under the [MIT License](LICENSE).