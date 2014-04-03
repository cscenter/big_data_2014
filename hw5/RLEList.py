import itertools

class RLEList(object):
    def __init__(self):
        pass

    def append(self, value):
        pass

    def insert(self, index, value):
        pass

    def get(self, index):
        pass

    def iterator(self):
        pass

class RLEListRefImpl(RLEList):
    def __init__(self):
        self.impl = []

    def append(self, value):
        self.impl.append(value)

    def insert(self, index, value):
        self.impl.insert(index, value)

    def get(self, index):
        return self.impl[index]

    def iterator(self):
        return iter(self.impl)

def group(iterable):
    data = []
    for x in iterable:
        if not data:
            data.append((x, 1))
            continue
        elt, count = data[-1]
        if elt == x:
            data[-1] = (elt, count + 1)
            continue
        data.append((x, 1))
    return data

class RLEListImpl(RLEList):

    def __init__(self, iterable = []):
        self.data = group(iterable)
        self.size = sum((count for _, count in self.data))

    def append(self, value):
        idx = len(self.data)
        self.data.append((value, 1))
        self.__join(idx)
        self.size += 1

    def insert(self, index, value):
        if index >= self.size:
            self.append(value)
            return
        block_idx, idx_in_block = self.__find_block_by_idx(index)
        self.__insert(block_idx, idx_in_block, value)
        self.size += 1

    def get(self, index):
        return self[index]

    def iterator(self):
        return self.__iter__()

    # implementation

    def __insert(self, block_idx, idx_in_block, value):
        if self.__insert_without_split(block_idx, idx_in_block, value):
            return
        self.__insert_with_split(block_idx, idx_in_block, value)

    def __insert_without_split(self, block_idx, idx_in_block, value):
        def can_insert_before(block_idx, idx_in_block):
            return idx_in_block == 0 and block_idx > 0

        def try_add_to_block(self, idx, value):
            elt, count = self.data[idx]
            if elt == value:
                self.data[idx] = (elt, count + 1)
                return True
            return False

        if can_insert_before(block_idx, idx_in_block):
            if try_add_to_block(block_idx - 1, value):
                return True
        return try_add_to_block(block_idx, value)

    def __insert_with_split(self, block_idx, idx_in_block, value):
        target_idx = self.__split(block_idx, idx_in_block)
        self.data.insert(target_idx, (value, 1))
        self.__join(target_idx)

    def __split(self, block_idx, split_idx):
        if split_idx == 0:
            return block_idx
        elt, count = self.data[block_idx]
        assert count > split_idx
        self.data[block_idx] = (elt, split_idx)
        self.data.insert(block_idx + 1, (elt, count - split_idx))
        return block_idx + 1

    def __join(self, block_idx):
        def join_at(left_idx):
            right_idx = left_idx + 1
            left_elt, left_count = self.data[left_idx]
            right_elt, right_count = self.data[right_idx]
            if left_elt != right_elt:
                return False
            self.data[left_idx] = (left_elt, left_count + right_count)
            self.data.pop(right_idx)
            return True

        assert block_idx >= 0 and block_idx < len(self.data)
        if block_idx > 0 and join_at(block_idx - 1):
            return
        if block_idx + 1 < len(self.data) and join_at(block_idx):
            return

    def __find_block_by_idx(self, index):
        start_idx = 0
        for block_idx, (_, count) in enumerate(self.data):
            if index < start_idx + count:
                return (block_idx, index - start_idx)
            start_idx += count
        raise IndexError("RLEList index out of range")

    # standard magic methods

    def __len__(self):
        return self.size

    def __iter__(self):
        for elt, count in self.data:
            for _ in xrange(count):
                yield elt

    def __repr__(self):
        return "RLEListImpl({})".format(self.data)

    def __getitem__(self, idx):
        block_idx, _ = self.__find_block_by_idx(index)
        return self.data[block_idx][0]


def demo():
    list = RLEListRefImpl()
    list.append("foo")
    list.insert(0, "bar")
    print list.iterator().next()
    print list.get(1)
