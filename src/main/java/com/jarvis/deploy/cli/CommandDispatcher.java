package com.jarvis.deploy.cli;

import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.deployment.DeploymentService;
import com.jarvis.deploy.environment.Environment;
import com.jarvis.deploy.rollback.RollbackService;

import java.util.HashMap;
import java.util.Map;

/**
 * Dispatches CLI commands to the appropriate service handlers.
 */
public class CommandDispatcher {

    private final DeploymentService deploymentService;
    private final RollbackService rollbackService;
    private final AuditLogger auditLogger;
    private final Map<String, CommandHandler> handlers = new HashMap<>();

    public CommandDispatcher(DeploymentService deploymentService,
                             RollbackService rollbackService,
                             AuditLogger auditLogger) {
        this.deploymentService = deploymentService;
        this.rollbackService = rollbackService;
        this.auditLogger = auditLogger;
        registerHandlers();
    }

    private void registerHandlers() {
        handlers.put("deploy", args -> {
            if (args.length < 3) {
                return "Usage: deploy <env> <version> <artifact>";
            }
            Environment env = new Environment(args[0]);
            String version = args[1];
            String artifact = args[2];
            deploymentService.deploy(env, version, artifact);
            auditLogger.log("deploy", "Deployed " + artifact + " v" + version + " to " + args[0]);
            return "Deployment initiated: " + artifact + " v" + version + " -> " + args[0];
        });

        handlers.put("rollback", args -> {
            if (args.length < 2) {
                return "Usage: rollback <env> <deploymentId>";
            }
            Environment env = new Environment(args[0]);
            String deploymentId = args[1];
            rollbackService.rollback(env, deploymentId);
            auditLogger.log("rollback", "Rolled back deployment " + deploymentId + " in " + args[0]);
            return "Rollback completed for deployment " + deploymentId + " in " + args[0];
        });

        handlers.put("status", args -> {
            if (args.length < 1) {
                return "Usage: status <env>";
            }
            Environment env = new Environment(args[0]);
            return deploymentService.getStatus(env).toString();
        });

        handlers.put("help", args ->
            "Available commands:\n" +
            "  deploy <env> <version> <artifact>  - Deploy an artifact\n" +
            "  rollback <env> <deploymentId>      - Rollback a deployment\n" +
            "  status <env>                        - Show environment status\n" +
            "  help                                - Show this help message"
        );
    }

    public String dispatch(String command, String[] args) {
        CommandHandler handler = handlers.get(command.toLowerCase());
        if (handler == null) {
            return "Unknown command: '" + command + "'. Run 'help' for available commands.";
        }
        try {
            return handler.handle(args);
        } catch (Exception e) {
            auditLogger.log("error", "Command '" + command + "' failed: " + e.getMessage());
            return "Error executing command '" + command + "': " + e.getMessage();
        }
    }

    @FunctionalInterface
    interface CommandHandler {
        String handle(String[] args) throws Exception;
    }
}
