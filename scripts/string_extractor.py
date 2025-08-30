# /// script
# requires-python = ">=3.10"
# dependencies = [
#     "lingua-language-detector",
# ]
# ///

import os
import re
import sys
import xml.etree.ElementTree as ET

from lingua import Language, LanguageDetectorBuilder


def detect_language(text, target_language):
    try:
        detector = LanguageDetectorBuilder.from_all_languages().build()
        detected_lang = detector.detect_language_of(text)
        return detected_lang == target_language
    except Exception as e:
        print(f"Language detection error for text '{text}': {e}")
        return False


def extract_from_file(file_path, target_language):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        android_ns = "{http://schemas.android.com/apk/res/android}"

        def process_element(elem):
            elem_results = []

            text_attr = elem.get(android_ns + "text")
            if text_attr and detect_language(text_attr, target_language):
                elem_results.append(text_attr)
            hint_attr = elem.get(android_ns + "hint")
            if hint_attr and detect_language(hint_attr, target_language):
                elem_results.append(hint_attr)

            if elem.tag == "string" and detect_language(elem.text, target_language):
                elem_results.append(elem.text)

            for child in elem:
                elem_results.extend(process_element(child))

            return elem_results

        return process_element(root)
    except ET.ParseError as e:
        print(f"Parse error in {file_path}: {e}")
        return []
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return []


STRING_PATTERN = re.compile(r'const-string(?:/jumbo)?\s+[vp]\d+,\s+"((?:[^"\\]|\\.)*)"')


def extract_from_smali(file_path, target_language):
    result = []
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
        matches = STRING_PATTERN.findall(content)
        for match in matches:
            # suppose const-string v1, "\ub2e4\uc6b4\ub85c\ub4dc\ud560 \uc218 \uc788\ub294 \uc791\ud488\uc774 \uc5c6\uc2b5\ub2c8\ub2e4."
            # this will encode "\ub2e4\uc6b4\ub85c\ub4dc\ud560 \uc218 \uc788\ub294 \uc791\ud488\uc774 \uc5c6\uc2b5\ub2c8\ub2e4."
            # into "다운로드할 수 있는 작품이 없습니다."
            match = match.encode("raw_unicode_escape").decode("unicode_escape")
            if detect_language(match, target_language):
                result.append(match)
    return result


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python string_extractor.py <layout_directory> <smali_directory>")
        sys.exit(1)

    layout_directory = sys.argv[1]
    smali_directory = sys.argv[2]

    target_language = Language.KOREAN

    if layout_directory != "_":
        data = []

        for root_dir, _, files in os.walk(layout_directory):
            for file in files:
                if file.endswith(".xml"):
                    full_path = os.path.join(root_dir, file)
                    extracts = extract_from_file(full_path, target_language)
                    data.extend(extracts)

        if not data:
            print(f"No texts found matching language '{target_language}'")
        else:
            with open("data/layout_string.csv", "w", encoding="UTF-8") as f:
                for text in set(data):
                    text = text.replace("\n", "\\n")
                    f.write(f"{text}|{text}\n")
            print("Output written to data/layout_string.csv")

    if smali_directory != "_":
        smali_str = []
        for root_dir, _, files in os.walk(smali_directory):
            for file in files:
                if file.endswith(".smali"):
                    full_path = os.path.join(root_dir, file)
                    extracts = extract_from_smali(full_path, target_language)
                    smali_str.extend(extracts)
        if not smali_str:
            print(f"No texts found matching language '{target_language}'")
        else:
            with open("data/smali_string.csv", "w", encoding="UTF-8") as f:
                for text in set(smali_str):
                    text = text.replace("\n", "\\n")
                    f.write(f"{text}|{text}\n")
            print("Output written to data/smali_string.csv")
