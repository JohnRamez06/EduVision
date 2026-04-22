class FrameIterator:
    """
    Iterator pattern: yields frames from a VideoStream-like object that has .read().

    Usage:
      for frame in FrameIterator(stream):
          ...
    """

    def __init__(self, stream, max_frames=None):
        self.stream = stream
        self.max_frames = max_frames
        self._count = 0

    def __iter__(self):
        return self

    def __next__(self):
        if self.max_frames is not None and self._count >= self.max_frames:
            raise StopIteration

        frame = self.stream.read()
        if frame is None:
            raise StopIteration

        self._count += 1
        return frame