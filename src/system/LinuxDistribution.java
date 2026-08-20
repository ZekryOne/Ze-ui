package system;

public record LinuxDistribution(String id, String name, String packageManager) {
    public boolean isDebianFamily() {
        return id.equals("debian") || id.equals("ubuntu") || id.equals("linuxmint") || id.equals("pop");
    }
}
