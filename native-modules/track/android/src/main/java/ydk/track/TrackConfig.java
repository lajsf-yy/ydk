package ydk.track;

import ydk.annotations.YdkConfigNode;
import ydk.annotations.YdkConfigValue;

@YdkConfigNode
public class TrackConfig {

    @YdkConfigValue(name = "name")
    private String name;

    public String getName() {
        return name;
    }
}
