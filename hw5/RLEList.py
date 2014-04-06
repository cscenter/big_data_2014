from bisect import bisect_left


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


class Data:
    def __init__(self, v, c, i):
        self.value = v
        self.count = c
        self.start_index = i

    def __cmp__(self, other):
        if self.start_index + self.count <= other:
            return -1
        if self.start_index > other:
            return 1
        return 0

    def __repr__(self):
        return "(%s, %d, %d)" % (str(self.value), self.count, self.start_index)


class RLEListRefImpl(RLEList):
    def __init__(self):
        self.data = []

    def append(self, value):
        if not self.data:
            self.data.append(Data(value, 1, 0))
        else:
            size = len(self.data)
            last = self.data[size - 1]
            if last.value == value:
                last.count += 1
            else:
                self.data.append(Data(value, 1, last.start_index + last.count))

    def insert(self, index, value):
        if index < 0:
            raise IndexError("Index must be positive")
        size = self.__len__()
        if index > size:
            raise IndexError("index must be less than array size")
        if index == size:
            self.append(value)
            return
        index_to_paste = bisect_left(self.data, index)
        el = self.data[index_to_paste]
        if el.value == value:
            el.count += 1
            for i in self.data[index_to_paste + 1:]:
                i.start_index += 1
            return
        if index == el.start_index:
            if index_to_paste != 0 and value == self.data[index_to_paste - 1].value:
                self.data[index_to_paste - 1].count += 1
            else:
                self.data.insert(index_to_paste, Data(value, 1, el.start_index))
                index_to_paste += 1
            for i in self.data[index_to_paste:]:
                i.start_index += 1
            return
        tmp = el.count
        el.count = index - el.start_index
        self.data.insert(index_to_paste + 1, Data(value, 1, index))
        self.data.insert(index_to_paste + 2, Data(el.value, tmp - el.count, index + 1))
        for i in self.data[index_to_paste + 3:]:
            i.start_index += 1


    def __len__(self):
        if not self.data:
            return 0
        last = self.data[len(self.data) - 1]
        return last.start_index + last.count

    def get(self, index):
        if index < 0:
            raise IndexError("index must be positive")
        last = self.data[len(self.data) - 1]
        size = last.start_index + last.count
        if index >= size:
            raise IndexError("index must be less than array size")
        return self.data[bisect_left(self.data, index)].value

    class RLEListIterator:
        def __init__(self, lst):
            self.i = 0
            self.t = 0
            self.list = lst

        def next(self):
            el = self.list.data[self.i]
            if self.t < el.count:
                self.t += 1
                return el.value
            else:
                self.i += 1
                self.t = 1
                if self.i == len(self.list.data):
                    raise StopIteration()
                return self.list.data[self.i].value

    def iterator(self):
        return self.RLEListIterator(self)

    def __iter__(self):
        return self.iterator()


def demo():
    list = RLEListRefImpl()
    list.append("foo")
    list.insert(0, "bar")
    print list.iterator().next()
    print list.get(1)


if __name__ == "__main__":
    lst = RLEListRefImpl()
    lst.append(0)
    lst.append(0)
    lst.append(1)
    for i in lst.data:
        print i.value, i.count, i.start_index
    print lst.get(0), lst.get(1), lst.get(2)
    lst.insert(0, 1)
    lst.insert(4, 4)

    print lst.data
