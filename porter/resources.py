"""Memory-aware concurrency defaults for Android porting.

The port runs several memory-heavy native/.NET/Python stages. Android reports
system RAM through /proc/meminfo; desktop builds keep their existing CPU-based
parallelism.
"""
import os
import sys


def memory_mb():
    total = available = None
    try:
        with open('/proc/meminfo', encoding='ascii') as f:
            for line in f:
                if line.startswith('MemTotal:'):
                    total = int(line.split()[1]) / 1024
                elif line.startswith('MemAvailable:'):
                    available = int(line.split()[1]) / 1024
                if total is not None and available is not None:
                    break
    except (OSError, ValueError):
        pass
    return total, available


def is_android():
    return (sys.platform == 'android' or hasattr(sys, 'getandroidapilevel') or
            bool(os.environ.get('ANDROID_ROOT') or os.environ.get('ANDROID_DATA')))


def safe_jobs(requested=None, *, total_mb=None, available_mb=None, cpu_count=None, android=None):
    """Resolve a worker count that will not fan out memory-heavy stages on Android.

    Eight-GiB-class phones get one worker: shader splitting forks workers, Vulkan
    translation starts a .NET process per worker, and Zstandard also uses workers.
    A caller-supplied --jobs is an upper bound, never permission to exceed the
    device's memory-derived safety ceiling.
    """
    cpu = max(1, int(cpu_count or os.cpu_count() or 1))
    wanted = cpu if requested is None else max(1, int(requested))
    if android is None:
        android = is_android()
    if not android:
        return min(wanted, cpu)
    if total_mb is None:
        total_mb, detected_available = memory_mb()
        if available_mb is None:
            available_mb = detected_available
    if total_mb is None:
        limit = 1  # unknown Android memory: fail safe, not fast
    elif total_mb <= 8192:
        limit = 1
    elif total_mb <= 12288:
        limit = 2
    elif total_mb <= 16384:
        limit = 3
    else:
        limit = min(cpu, 4)
    if available_mb is not None:
        if available_mb < 3072:
            limit = 1
        elif available_mb < 4096:
            limit = min(limit, 2)
    return max(1, min(wanted, limit, cpu))
