package app.usecase.job;

/**
 * ジョブタスクのを表現するマーカーインターフェース
 */
public interface JobTask {

    /**
     * ジョブタスクを実行します
     */
    void execute() throws Exception;
}
