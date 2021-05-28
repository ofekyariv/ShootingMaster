package ori.project.shootingmaster;

public class Score implements Comparable {
    String key;
    private String userName;
    private int score;

    public Score() {
        userName = "";
        score = 0;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int compareTo(Object o) {
        Score temp = (Score) o;
        if (key.equals(temp.getKey()))
            return 1;
        else return 0;
    }

}
