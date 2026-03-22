package app.context.logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import app.context.http.RequestContext;

/**
 * 監査ログ出力用クラス
 * メソッドに@Auditを付与したメソッドの実行前後のログを出力します。
 */
@Aspect
@Component
public class AuditLogInterceptor {

    @Around("@annotation(app.context.anotation.Audit)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        String api = joinPoint.getSignature().getName();
        String ipAddress = RequestContext.getClientIpAddress();
        String userAgent = RequestContext.getUserAgent();

        // 実行前ログ
        AppLogger.info(
                joinPoint.getTarget().getClass(),
                "{}, {}, {}",
                ipAddress,
                api,
                userAgent);

        try {
            // メソッド実行
            Object result = joinPoint.proceed();

            // 成功時ログ
            AppLogger.info(
                    joinPoint.getTarget().getClass(),
                    "{}, {}, SUCCESS",
                    ipAddress,
                    api);

            return result;
        } catch (Exception e) {
            // エラー時ログ
            AppLogger.error(
                    joinPoint.getTarget().getClass(),
                    "{}, {}, ERROR:{}",
                    ipAddress,
                    api,
                    e.getMessage());
            throw e;
        }
    }
}
