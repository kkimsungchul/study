import time
import logging
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.logs import LoggerProvider, BatchLogProcessor
from opentelemetry.sdk.logs.export import BatchLogProcessor, OTLPLogExporter

# OpenTelemetry 설정
resource = Resource.create({"service.name": "log-collector-service"})

trace.set_tracer_provider(TracerProvider(resource=resource))
tracer = trace.get_tracer(__name__)
span_exporter = OTLPSpanExporter(endpoint="http://localhost:9999", insecure=True)
span_processor = BatchSpanProcessor(span_exporter)
trace.get_tracer_provider().add_span_processor(span_processor)

logger_provider = LoggerProvider(resource=resource)
log_exporter = OTLPLogExporter(endpoint="http://localhost:9999", insecure=True)
log_processor = BatchLogProcessor(log_exporter)
logger_provider.add_log_processor(log_processor)

class LogHandler(FileSystemEventHandler):
    def on_modified(self, event):
        if event.src_path == "C:\\ConvertedLogs\\Application.log":
            with open(event.src_path, "r") as log_file:
                lines = log_file.readlines()
                for line in lines:
                    logger_provider.get_logger(__name__).emit(line.strip())

if __name__ == "__main__":
    path = "C:\\ConvertedLogs\\Application.log"
    event_handler = LogHandler()
    observer = Observer()
    observer.schedule(event_handler, path, recursive=False)
    observer.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()
