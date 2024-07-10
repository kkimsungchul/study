import time
from pathlib import Path
from opentelemetry import trace
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk._logs import (
    LoggerProvider,
    LogRecord,
)
from opentelemetry.sdk._logs.export import BatchLogRecordProcessor
from opentelemetry.exporter.otlp.proto.grpc._log_exporter import OTLPLogExporter
from opentelemetry.sdk.resources import SERVICE_NAME, Resource
from opentelemetry.trace import Status, StatusCode

# OpenTelemetry 설정
resource = Resource(attributes={
    SERVICE_NAME: "log-collector-service"
})

# 트레이스 설정
trace.set_tracer_provider(TracerProvider(resource=resource))
tracer = trace.get_tracer(__name__)
span_exporter = OTLPSpanExporter(endpoint="http://localhost:9999")
span_processor = BatchSpanProcessor(span_exporter)
trace.get_tracer_provider().add_span_processor(span_processor)

# 로그 설정
logger_provider = LoggerProvider(resource=resource)
log_exporter = OTLPLogExporter(endpoint="http://localhost:9999")
log_processor = BatchLogRecordProcessor(log_exporter)
logger_provider.add_log_record_processor(log_processor)
logger = logger_provider.get_logger(__name__)

def tail_file(filepath):
    with open(filepath, 'r') as file:
        file.seek(0, 2)  # 파일의 끝으로 이동
        while True:
            line = file.readline()
            if not line:
                time.sleep(0.1)  # 새 라인이 없으면 잠시 대기
                continue
            yield line

def main():
    log_file = Path(r"C:\ConvertedLogs\Application.log")
    
    print(f"Monitoring {log_file}. Press Ctrl+C to stop.")
    
    with tracer.start_as_current_span("log_monitoring") as root_span:
        for line in tail_file(log_file):
            with tracer.start_as_current_span("process_log_line"):
                current_span = trace.get_current_span()
                span_context = current_span.get_span_context()
                log_record = LogRecord(
                    timestamp=int(time.time() * 1_000_000_000),  # 나노초 단위
                    trace_id=span_context.trace_id,
                    span_id=span_context.span_id,
                    trace_flags=span_context.trace_flags,
                    severity_text="INFO",
                    body=line.strip(),
                    attributes={}
                )
                logger.emit(log_record)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Stopping log collection...")
    finally:
        logger_provider.shutdown()
        trace.get_tracer_provider().shutdown()