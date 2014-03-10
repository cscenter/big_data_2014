
def do_map(text):
    elements = {}
    do_map_with_counts(text, elements)
    return elements

def do_map_with_counts(text, elements):
    is_left = True
    numbers = text.split(' ')
    metadata = numbers[0]
    if (metadata.startswith('r')):
        is_left = False
    numbers.remove(metadata)
    line_number = int(metadata[2:])
    count = 1

    for num in numbers:
        val = int(num)
        key = 0
        if (is_left):
            key = count
            val = tuple([1, val, line_number])
        else:
            key = line_number
            val = tuple([0, val, count])

        if key not in elements.keys():
            elements[key] = []
        elements[key].append(val)
        count += 1