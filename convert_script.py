import sys
import os
from pdf2docx import Converter

def convert_pdf_to_docx(pdf_path, docx_path):
    try:
        # Khởi tạo converter
        cv = Converter(pdf_path)
        # Thực hiện convert (start=0, end=None nghĩa là convert tất cả trang)
        cv.convert(docx_path, start=0, end=None)
        cv.close()
        print("SUCCESS") # Dấu hiệu để Java nhận biết thành công
    except Exception as e:
        print(f"ERROR: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python convert_script.py <input_pdf> <output_docx>")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    
    convert_pdf_to_docx(input_file, output_file)