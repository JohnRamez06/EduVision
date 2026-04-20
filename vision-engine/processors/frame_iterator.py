# ITERATOR PATTERN - walks frames one by one
class FrameIterator:
    def __init__(self, source):
        self.source = source
        self._buffer = []
        self._index = 0
    def __iter__(self): return self
    def __next__(self):
        frame = self.source.read()
        if frame is None: raise StopIteration
        return frame
