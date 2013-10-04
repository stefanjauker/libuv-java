import net.java.libuv.LibUV;
import net.java.libuv.SignalCallback;
import net.java.libuv.handles.LoopHandle;
import net.java.libuv.handles.SignalHandle;

public class SignalHandleTest {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");

    static {
        // call a LibUV method just to ensure that the native lib is loaded
        System.out.println(SignalHandleTest.class.getSimpleName() + " in " + LibUV.cwd());
    }

    // this test needs to be run manually, so no @Test annotation
    public static void main(String[] args) throws Exception {
        if (IS_WINDOWS) {
            System.err.println("Sorry this test does not work on windows");
            return;
        }
        final LoopHandle loop = new LoopHandle();
        final SignalHandle handle = new SignalHandle(loop);
        handle.setSignalCallback(new SignalCallback() {
            @Override
            public void call(int signum) throws Exception {
                assert signum == 28;
                System.out.println("received signal " + signum);
            }
        });
        handle.start(28);
        System.out.println("waiting for signals... ");
        System.out.println("  (try kill -WINCH <pid>, kill -28 <pid>, or just resize the console)");
        loop.run();
    }

}
