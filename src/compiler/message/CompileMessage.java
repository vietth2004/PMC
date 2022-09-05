package compiler.message;

public class CompileMessage implements ICompileMessage {
    private String message;
    private String filePath;
    private String compilationCommand;
    private String linkingCommand;

    public CompileMessage(String mess, String path) {
        this.message = mess;
        this.filePath = path;
    }

    @Override
    public MessageType getType() {
        if (message.contains(" error:"))
            return MessageType.ERROR;
        else if (message.contains(" warning:"))
            return MessageType.WARNING;
        else if (message.length() == 0)
            return MessageType.EMPTY;
        else
            return MessageType.UNDEFINED;
    }

    public void setCompilationCommand(String compilationCommand) {
        this.compilationCommand = compilationCommand;
    }

    @Override
    public String getLinkingCommand() {
        return linkingCommand;
    }

    @Override
    public void setLinkingCommand(String linkingCommand) {
        this.linkingCommand = linkingCommand;
    }

    @Override
    public String getCompilationCommand() {
        return compilationCommand;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return message;
    }

}
