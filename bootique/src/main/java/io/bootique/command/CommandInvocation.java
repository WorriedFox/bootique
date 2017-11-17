package io.bootique.command;

import java.util.Objects;

/**
 * Contains a "recipe" for invoking a command with preset arguments.
 *
 * @since 0.25
 */
public abstract class CommandInvocation {

    private final String[] args;
    private final boolean terminateOnErrors;

    protected CommandInvocation(String[] args, boolean terminateOnErrors) {
        this.args = args;
        this.terminateOnErrors = terminateOnErrors;
    }

    /**
     * Starts building an invocation with a named command.
     *
     * @param fullCommandName full name of the command.
     */
    public static Builder forName(String fullCommandName) {
        return new Builder(fullCommandName);
    }

    /**
     * Starts building an invocation with a command of a known type.
     */
    public static Builder forType(Class<? extends Command> commandType) {
        return new Builder(commandType);
    }

    public abstract String getCommandName(CommandManager manager);

    public String[] getArgs() {
        return args;
    }

    /**
     * @return true, if Bootique program should terminate, when this invocation fails
     */
    public boolean shouldTerminateOnErrors() {
        return terminateOnErrors;
    }

    static class ByNameInvocation extends CommandInvocation {
        private String commandName;

        ByNameInvocation(String commandName, String[] args, boolean terminateOnErrors) {
            super(args, terminateOnErrors);
            this.commandName = commandName;
        }

        @Override
        public String getCommandName(CommandManager manager) {
            // TODO: should we pass this through CommandManager to ensure the name is valid?
            return commandName;
        }
    }

    static class ByTypeInvocation extends CommandInvocation {
        private Class<? extends Command> commandType;

        ByTypeInvocation(Class<? extends Command> commandType, String[] args, boolean terminateOnErrors) {
            super(args, terminateOnErrors);
            this.commandType = commandType;
        }

        @Override
        public String getCommandName(CommandManager manager) {
            return manager.lookupByType(commandType).getMetadata().getName();
        }
    }

    /**
     * @since 0.25
     */
    public static class Builder {

        private static final String[] NO_ARGS = new String[0];

        private Class<? extends Command> commandType;
        private String commandName;

        private String[] args = NO_ARGS;
        private boolean terminateOnErrors;

        protected Builder(String commandName) {
            this.commandName = Objects.requireNonNull(commandName);
        }

        protected Builder(Class<? extends Command> commandType) {
            this.commandType = Objects.requireNonNull(commandType);
        }

        /**
         * Set command line arguments for this invocation
         */
        public Builder arguments(String[] args) {
            this.args = args != null ? args : NO_ARGS;
            return this;
        }

        /**
         * Indicate, that Bootique program should terminate, when this invocation fails.
         */
        public Builder terminateOnErrors() {
            this.terminateOnErrors = true;
            return this;
        }

        public CommandInvocation build() {
            return commandType != null
                    ? new ByTypeInvocation(commandType, args, terminateOnErrors)
                    : new ByNameInvocation(commandName, args, terminateOnErrors);
        }
    }
}
