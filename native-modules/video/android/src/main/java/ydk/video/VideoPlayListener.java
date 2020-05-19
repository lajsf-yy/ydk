package ydk.video;

/**
 * Created by Gsm on 2018/6/6.
 */
public interface VideoPlayListener {

    void progress(long progress, long duration);

    void buffering();

    void ready();

    void stalled();

    void end();

    void close();

    void error(Exception e);
}
