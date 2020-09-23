package java.awt;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-21
 */
public class GraphicsEnvironment {

    public static GraphicsEnvironment getLocalGraphicsEnvironment(){
        return null;
    }

    public boolean isHeadlessInstance(){
        return false;
    }

    public GraphicsDevice[] getScreenDevices(){
        return new GraphicsDevice[0];
    }
}
