package net.codestorage.picspot;

public class locate {
    private String title;
    private String imgResource;

    public locate(String title, String imgResource) {
        setImgResource(imgResource);
        setTitle(title);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgResource() {
        return imgResource;
    }

    public void setImgResource(String imgResource) {
        this.imgResource = imgResource;
    }
}
