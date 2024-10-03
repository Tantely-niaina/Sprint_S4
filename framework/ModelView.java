package frameworks;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    private String url;
    private HashMap<String, Object> data;

    public ModelView() {
        this.data = new HashMap<>();
    }

    public ModelView(String url) {
        this.url = url;
        this.data = new HashMap<>();
    }

    public String getUrl() {
        return url;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void addObject(String key, Object value) {
        this.data.put(key, value);
    }

    public String getSingleKey() {
        if (this.data.size() == 1) {
            for (Map.Entry<String, Object> entry : this.data.entrySet()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Object getSingleValue() {
        if (this.data.size() == 1) {
            for (Map.Entry<String, Object> entry : this.data.entrySet()) {
                return entry.getValue();
            }
        }
        return null;
    }
}
